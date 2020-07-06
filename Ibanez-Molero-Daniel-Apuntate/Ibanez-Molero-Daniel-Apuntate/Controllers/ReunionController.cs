using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Reflection;
using System.Text;
using Ibanez_Molero_Daniel_Apuntate.Models;
using Ibanez_Molero_Daniel_Apuntate.Repositories;
using MongoDB.Bson;
using Newtonsoft.Json.Linq;
using RabbitMQ.Client;
using Syncfusion.XlsIO;

namespace Ibanez_Molero_Daniel_Apuntate.Controllers
{
    public class ReunionController
    {
        private RepositorioReuniones _repositorio;
        private IModel channel;

        public ReunionController()
        {
            _repositorio = new RepositorioReuniones();
            var factory = new ConnectionFactory() { Uri = new Uri("amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy") };
            channel = factory.CreateConnection().CreateModel();
            channel.QueueDeclare(queue: "ArSo",
                durable: true,
                exclusive: false,
                autoDelete: false,
                arguments: null);
        }

        public string CreateReunion(Reunion r)
        {
            if (r == null)
                throw new ReunionException("La reunion no puede ser nula");
            if (r.Intervalos <= 0)
                throw new ReunionException("El numero de alumnos por intervalo ha de ser mayor que cero");
            if (r.Componentes <= 0)
                throw new ReunionException("El numero de alumnos ha de ser mayor que cero");
            if (r.Fin.CompareTo(r.Inicio) < 0)
                throw new ReunionException("Las horas de inicio y fin han de ser coherentes");
            if (r.Cierre.CompareTo(r.Apertura) < 0)
                throw new ReunionException("Las fechas de apertura y cierre han de ser coherentes");
            if (r.Organizador != null)
            {
                if (!GetRol(r.Organizador).Equals("profesor"))

                    throw new ReunionException("El campo organizador ha de ser el correo de un profesor valido");

            }
            else throw new ReunionException("El campo organizador no puede ser nulo");

            // Calculo de tiempos y creación de los grupos disponibles
            var tiempoDeIntervalo = r.Fin.Subtract(r.Inicio).TotalSeconds / (r.Componentes / r.Intervalos);
            var componentes = new BsonArray();
            int idGrupo = 0;
            for (DateTime horaGrupo = r.Inicio; horaGrupo.CompareTo(r.Fin) < 0; horaGrupo = horaGrupo.AddSeconds(tiempoDeIntervalo))
            {
                for (int i = 0; i < r.Intervalos; i++)
                {
                    componentes.Add(new BsonDocument
                    {
                        {"grupo", idGrupo},
                        {"hora", horaGrupo},
                        {"correo", "PLAZA_DISPONIBLE"},
                    });
                }
                idGrupo++;
            }
            var grupos = new BsonDocument
            {
                {"plazasDisponibles", r.Componentes},
                {"componentesPorGrupo", r.Intervalos},
                {"tiempoPorIntervalo", tiempoDeIntervalo},
                {"componentes", componentes}
            };
            // Crea la reunion con los grupos definidos
            var reunion = new BsonDocument
            {
                {"titulo", r.Titulo}, {"organizador", r.Organizador},
                {"ubicacion", r.Ubicacion}, {"categoria", r.Categoria}, {"descripcion", r.Descripcion},
                {"inicio", r.Inicio}, {"fin", r.Fin}, {"frecuencia", r.Frecuencia},
                {"apertura", r.Apertura},
                {"cierre", r.Cierre}, {"grupos", grupos}, {"tipo", r.Tipo}
            };
            return _repositorio.SaveReunion(reunion);
        }

        public string GetReunion(string id)
        {
            ObjectId mongoId;
            if ((id != null) && !id.Equals(""))
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            return _repositorio.GetReunion(mongoId).ToJson();
        }

        public string GetAllReuniones() => _repositorio.GetAll().ToJson();

        public void RemoveReunion(string id, string correo)
        {
            ObjectId mongoId;
            if ((id != null) && !id.Equals(""))
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            
            if (correo != null)
            {
                if (!GetRol(correo).Equals("profesor"))
                    throw new ReunionException("El correo ha de pertenecer a un profesor valido");
            }
            else throw new ReunionException("El correo no puede ser nulo");
            if (!_repositorio.GetReunion(mongoId)["organizador"].Equals(correo))
                throw new ReunionException("El correo ha de pertenecer a el organizador de la reunion");
            _repositorio.RemoveReunion(mongoId);
        }

        public void OcuparPlaza(string id, string correo, int grupo)
        {
            ObjectId mongoId;
            if ((id != null) && !id.Equals(""))
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            
            if (correo != null)
            {
                if (!GetRol(correo).Equals("estudiante"))
                    throw new ReunionException("El correo ha de pertenecer a un estudiante valido");
            }
            else throw new ReunionException("El correo no puede ser nulo");

            //Recupera los grupos de la reunion para comprobar si esta disponible la seleccion
            BsonDocument reunion = _repositorio.GetReunion(mongoId).AsBsonDocument;
            BsonDocument grupos = reunion["grupos"].AsBsonDocument;
            BsonArray componentes = grupos["componentes"].AsBsonArray;
            bool usuarioIntroducido = false;
            BsonDocument grupoEnReunion = null;

            //Comprobacion de que el usuario no se encuentra ya apuntado en la reunión
            if (_repositorio.IsEspera(correo, id))
                throw new ReunionException("El usuario ya ha sido apuntado previamente a esta reunion");
            foreach (var bsonValue in componentes)
            {
                grupoEnReunion = (BsonDocument)bsonValue;
                if (grupoEnReunion["correo"].Equals(correo))
                    throw new ReunionException("El usuario ya ha sido apuntado previamente a esta reunion");
            }

            //Busca la plaza libre
            foreach (var bsonValue in componentes)
            {
                grupoEnReunion = (BsonDocument)bsonValue;
                if (grupoEnReunion["grupo"].Equals(grupo) && grupoEnReunion["correo"].Equals("PLAZA_DISPONIBLE"))
                {
                    grupoEnReunion.Set("correo", correo);
                    usuarioIntroducido = true;
                    break;
                }
            }
            //Si el usuario se ha introducido con éxito, actualizar el grupo y crear la tarea para el usuario
            if (usuarioIntroducido)
            {
                grupos = grupos.Set("componentes", componentes);
                grupos = grupos.Set("plazasDisponibles", grupos["plazasDisponibles"].AsInt32 - 1);
                reunion = reunion.Set("grupos", grupos);
                reunion = reunion.Set("_id", mongoId);
                SendAqmpMessage(reunion["organizador"] + ";" + correo + ";" + "REUNION" + ";" + reunion["ubicacion"] + ", " + grupoEnReunion["hora"]);
                _repositorio.UpdateReunion(mongoId, reunion);
            }
            //En caso contrario, añadir en la lista de espera a consultar en caso de que alguien libere la plaza
            else
            {
                _repositorio.SaveEspera(new BsonDocument
                {
                    {"reunion", id},
                    {"grupo", grupo},
                    {"correo", correo},
                });
            }
        }
        public void LiberarPlaza(string id, string correo)
        {
            ObjectId mongoId;
            if ((id != null) && !id.Equals(""))
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            if (correo != null)
            {
                if (!GetRol(correo).Equals("estudiante"))
                    throw new ReunionException("El correo ha de pertenecer a un estudiante valido");
            }
            else throw new ReunionException("El correo no puede ser nulo");

            //Si esta apuntado en lista de espera, solo se elimina de esta
            if (_repositorio.IsEspera(correo, id))
            {
                _repositorio.RemoveEspera(correo, id);
                return;
            }
            // Si no, recupera los grupos de la reunion para liberar la plaza
            BsonDocument reunion = _repositorio.GetReunion(mongoId).AsBsonDocument;
            BsonDocument grupos = reunion["grupos"].AsBsonDocument;
            BsonArray componentes = grupos["componentes"].AsBsonArray;
            foreach (var bsonValue in componentes)
            {
                var grupoEnReunion = (BsonDocument)bsonValue;
                if (grupoEnReunion["correo"].Equals(correo))
                {
                    BsonDocument inEspera = _repositorio.GetEspera(id, grupoEnReunion["grupo"].ToInt32());
                    // Si hay un alumno en la lista de pendientes, se introduce en su lugar, se elimina a este de la lista y se le informa
                    if (inEspera != null)
                    {
                        grupoEnReunion.Set("correo", inEspera["correo"].ToString());
                        _repositorio.RemoveEspera(inEspera["correo"].ToString(), id);
                        SendAqmpMessage(reunion["organizador"] + ";" + inEspera["correo"] + ";" + "REUNION" + ";" + reunion["ubicacion"] + ", " + grupoEnReunion["hora"]);
                    }
                    // Si no, se libera la plaza
                    else
                    {
                        grupoEnReunion.Set("correo", "PLAZA_DISPONIBLE");
                        grupos = grupos.Set("plazasDisponibles", grupos["plazasDisponibles"].AsInt32 + 1);
                    }
                    break;
                }
            }
            // Se actualiza la reunion con la nueva configuracion
            grupos = grupos.Set("componentes", componentes);
            reunion = reunion.Set("grupos", grupos);
            reunion = reunion.Set("_id", mongoId);
            _repositorio.UpdateReunion(mongoId, reunion);
        }

        public HttpResponseMessage ExportReuniones()
        {
            //Genera una coleccion de reuniones
            List<Reunion> reuniones = new List<Reunion>();
            foreach (var reunion in _repositorio.GetAll())
            {
                var r = new Reunion();
                r.Titulo = reunion["titulo"].ToString();
                r.Organizador = reunion["organizador"].ToString();
                r.Ubicacion = reunion["ubicacion"].ToString();
                r.Categoria = reunion["categoria"].ToString();
                r.Descripcion = reunion["descripcion"].ToString();
                r.Inicio = DateTime.Parse(reunion["inicio"].ToString());
                r.Fin = DateTime.Parse(reunion["fin"].ToString());
                r.Frecuencia = reunion["frecuencia"].ToString();
                r.Apertura = DateTime.Parse(reunion["apertura"].ToString());
                r.Cierre = DateTime.Parse(reunion["cierre"].ToString());
                r.Tipo = reunion["tipo"].ToString();
                reuniones.Add(r);
            }
            //traduce la coleccion a una datatable
            DataTable dataTable = new DataTable(typeof(Reunion).Name);
            PropertyInfo[] props = typeof(Reunion).GetProperties(BindingFlags.Public | BindingFlags.Instance);
            foreach (PropertyInfo prop in props)
            {
                dataTable.Columns.Add(prop.Name);
            }
            foreach (Reunion item in reuniones)
            {
                var values = new object[props.Length];
                for (int i = 0; i < props.Length; i++)
                {
                    values[i] = props[i].GetValue(item, null);
                }
                dataTable.Rows.Add(values);
            }
            //genera el documento Excel a partir de la datatable
            var stream = new MemoryStream();
            using (ExcelEngine excelEngine = new ExcelEngine())
            {
                IApplication application = excelEngine.Excel;
                application.DefaultVersion = ExcelVersion.Excel2016;
                IWorkbook workbook = application.Workbooks.Create(1);
                IWorksheet worksheet = workbook.Worksheets[0];
                worksheet.ImportDataTable(dataTable, true, 1, 1);
                worksheet.UsedRange.AutofitColumns();
                workbook.SaveAs(stream);
            }
            //Introduce el documento en el cuerpo de la respuesta http
            var result = new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new ByteArrayContent(stream.ToArray())
            };
            result.Content.Headers.ContentDisposition =
                new System.Net.Http.Headers.ContentDispositionHeaderValue("attachment")
                {
                    FileName = "reuniones.xlsx"
                };
            return result;
        }

        // Supporting methods
        private void SendAqmpMessage(string content)
        {
            string message = content;
            byte[] body = Encoding.UTF8.GetBytes(message);

            channel.BasicPublish(exchange: "",
                routingKey: "ArSo",
                basicProperties: null,
                body: body);
        }

        private string GetRol(string correo)
        {
            WebRequest request = WebRequest.Create("http://localhost:8083/api/usuarios/" + correo);
            request.Method = "POST";
            byte[] byteArray = Encoding.UTF8.GetBytes(correo);
            request.ContentType = "application/x-www-form-urlencoded";
            request.ContentLength = byteArray.Length;
            Stream dataStream = request.GetRequestStream();
            dataStream.Write(byteArray, 0, byteArray.Length);
            dataStream.Close();
            WebResponse response = request.GetResponse();
            string responseFromServer;
            using (dataStream = response.GetResponseStream())
            {
                StreamReader reader = new StreamReader(dataStream ?? throw new ReunionException("No se ha podido establecer la conexión con la base de datos de usuarios"));
                responseFromServer = reader.ReadToEnd();
            }
            response.Close();
            dynamic data = JObject.Parse(responseFromServer);
            return data.rol.chars;
        }
    }
}
