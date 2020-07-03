using System;
using Ibanez_Molero_Daniel_Apuntate.Models;
using MongoDB.Bson;
using Ibanez_Molero_Daniel_Apuntate.Repositories;
using Microsoft.Ajax.Utilities;

namespace Ibanez_Molero_Daniel_Apuntate.Controllers
{
    public class ReunionController
    {
        private RepositorioReuniones _repositorio;

        public ReunionController() => _repositorio = new RepositorioReuniones();

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
            var grupos = new BsonArray();
            var tiempoDeIntervalo = r.Fin.Subtract(r.Inicio).TotalMinutes / (r.Componentes / r.Intervalos);
            for (int i = 0; i < r.Componentes / r.Intervalos; i++)
            {
                var componentes = new BsonArray();
                grupos.Add(new BsonDocument
                {
                    {"plazasDisponibles", r.Intervalos},
                    {"tiempoDisponible", tiempoDeIntervalo},
                    {"componentes", componentes},
                });
            }
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
            ObjectId mongoId = new ObjectId();
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
            ObjectId mongoId = new ObjectId();
            if (!id.IsNullOrWhiteSpace())
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            _repositorio.RemoveReunion(mongoId);
        }

        public void OcuparPlaza(string id, string correo)
        {
            ObjectId mongoId = new ObjectId();
            if (!id.IsNullOrWhiteSpace())
            {
                if (!ObjectId.TryParse(id, out mongoId))
                    throw new ReunionException("Formato de Id incorrecto");
            }
            else throw new ReunionException("El id no puede ser nulo o vacio");
            //TODO (if correo != alumno...)
            var reunion = _repositorio.GetReunion(mongoId);
            var grupos= reunion["grupos"].AsBsonArray;
            foreach (var grupo in grupos)
            {
                if (Int16.Parse(grupo["plazasDisponibles"].ToString()) > 0)
                {
                    grupo["componentes"].AsBsonArray.Add(correo);
                    break;
                }
            }
            _repositorio.UpdateReunion(mongoId,reunion);
        }
    }
}
