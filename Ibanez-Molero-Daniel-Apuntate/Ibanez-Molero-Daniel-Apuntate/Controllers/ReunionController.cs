using System.IO;
using System.Net;
using System.Text;
using Ibanez_Molero_Daniel_Apuntate.Models;
using Ibanez_Molero_Daniel_Apuntate.Repositories;
using Microsoft.Ajax.Utilities;
using MongoDB.Bson;
//using RabbitMQ.Client;

namespace Ibanez_Molero_Daniel_Apuntate.Controllers
{
    public class ReunionController
    {
        private RepositorioReuniones _repositorio;
        //private IModel channel;

        public ReunionController()
        {
            _repositorio = new RepositorioReuniones();
            /*
            var factory = new ConnectionFactory() { Uri = new Uri("amqp://otbzkwgy:MUMAlBAj4iqa0y5ZX63cfDYX1hs7u00u@chinook.rmq.cloudamqp.com/otbzkwgy") };
            channel = factory.CreateConnection().CreateModel();
            */
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
            //TODO if r.getOrganizador is profesor...
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
            if (!id.IsNullOrWhiteSpace())
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            return _repositorio.GetReunion(mongoId).ToJson();
        }

        public string GetAllReuniones() => _repositorio.GetAll().ToJson();

        public void RemoveReunion(string id)
        {
            ObjectId mongoId;
            if (!id.IsNullOrWhiteSpace())
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            SendAQMPMessage("HOLA");
            _repositorio.RemoveReunion(mongoId);
        }

        public void OcuparPlaza(string id, string correo)
        {
            ObjectId mongoId;
            if (!id.IsNullOrWhiteSpace())
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            //TODO (if correo != alumno...)
            var reunion = _repositorio.GetReunion(mongoId).AsBsonDocument;
            var grupos = reunion["grupos"].AsBsonDocument;
            if (grupos["plazasDisponibles"].AsInt32 > 0)
            {
                //He intentado declarar componentes como BsonArray
                //Pero el método Add no funcionaba de ninguna manera
                var componentes = grupos["componentes"].AsBsonArray;
                componentes.Add(correo);
                grupos = grupos.Set("componentes", componentes);
                grupos = grupos.Set("plazasDisponibles", grupos["plazasDisponibles"].AsInt32 - 1);
            }
            else throw new ReunionException("La reunion ha de tener plazas libres");
            reunion = reunion.Set("grupos", grupos);
            reunion = reunion.Set("_id", mongoId);
            _repositorio.UpdateReunion(mongoId, reunion);
        }

        // Supporting methods
        //TODO
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
                StreamReader reader = new StreamReader(dataStream);
                responseFromServer = reader.ReadToEnd();
            }
            response.Close();
            return responseFromServer;
        }

        private void SendAQMPMessage(string content)
        {
            /*
            channel.QueueDeclare(queue: "ArSo",
            durable: false,
            exclusive: false,
            autoDelete: false,
            arguments: null);

            string message = content;
            var body = Encoding.UTF8.GetBytes(message);

            channel.BasicPublish(exchange: "",
            routingKey: "ArSo",
            basicProperties: null,
            body: body);
            */
        }
    }
}
