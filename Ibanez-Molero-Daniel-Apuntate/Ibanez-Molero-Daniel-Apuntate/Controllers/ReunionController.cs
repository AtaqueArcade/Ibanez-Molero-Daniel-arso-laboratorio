using System;
using System.IO;
using System.Net;
using System.Text;
using Ibanez_Molero_Daniel_Apuntate.Models;
using Ibanez_Molero_Daniel_Apuntate.Repositories;
using MongoDB.Bson;
using Newtonsoft.Json.Linq;
using RabbitMQ.Client;

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

            var tiempoDeIntervalo = r.Fin.Subtract(r.Inicio).TotalMinutes / (r.Componentes / r.Intervalos);
            var componentes = new BsonArray();
            var grupos = new BsonDocument
            {
                {"plazasDisponibles", r.Componentes},
                {"componentesPorGrupo", r.Intervalos},
                {"tiempoPorIntervalo", tiempoDeIntervalo},
                {"componentes", componentes}
            };
            var reunion = new BsonDocument
            {
                {"titulo", r.Titulo}, {"organizador", r.Organizador},
                {"ubicacion", r.Ubicacion}, {"categoria", r.Categoria}, {"descripcion", r.Descripcion},
                {"inicio", r.Inicio.ToString()}, {"fin", r.Fin.ToString()}, {"frecuencia", r.Frecuencia},
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

        public void OcuparPlaza(string id, string correo)
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
            var reunion = _repositorio.GetReunion(mongoId).AsBsonDocument;
            var grupos = reunion["grupos"].AsBsonDocument;
            if (grupos["plazasDisponibles"].AsInt32 > 0)
            {
                var componentes = grupos["componentes"].AsBsonArray;
                componentes.Add(correo);
                grupos = grupos.Set("componentes", componentes);
                grupos = grupos.Set("plazasDisponibles", grupos["plazasDisponibles"].AsInt32 - 1);
            }
            else throw new ReunionException("La reunion ha de tener plazas libres");

            reunion = reunion.Set("grupos", grupos);
            reunion = reunion.Set("_id", mongoId);
            SendAqmpMessage(reunion["organizador"] + ";" + correo + ";" + "REUNION" + ";" + reunion["ubicacion"] + ", " + reunion["ubicacion"] + ", " + reunion["inicio"] + "-" + reunion["fin"]);
            _repositorio.UpdateReunion(mongoId, reunion);
        }

        // Supporting methods
        private void SendAqmpMessage(string content)
        {
            string message = content;
            var body = Encoding.UTF8.GetBytes(message);

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
