using Ibanez_Molero_Daniel_Apuntate.Models;
using MongoDB.Bson;
using Ibanez_Molero_Daniel_Apuntate.Repositories;

namespace Ibanez_Molero_Daniel_Apuntate.Controllers
{
    public class ReunionController
    {
        private RepositorioReuniones _repositorio;

        public ReunionController()
        {
            _repositorio = new RepositorioReuniones();
        }

        public string CreateReunion(Reunion r)
        {
            //if r == null...
            //if r.getOrganizador is profesor...
            //if intervalos = 0... || componentes == 0... 

            var grupos = new BsonArray();
            var tiempoDeIntervalo = r.Fin.Subtract(r.Inicio).TotalMinutes / (r.Componentes / r.Intervalos);
            for (int i = 0; i < r.Componentes / r.Intervalos; i++)
                grupos.Add(new BsonDocument
                {
                    {"plazasDisponibles", r.Intervalos}, {"tiempoDisponible", tiempoDeIntervalo},
                });

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

        public string GetReunion(string id) => _repositorio.GetReunion(id).ToJson();

        public string GetAllReuniones() => _repositorio.GetAll().ToJson();

        public bool RemoveReunion(string id) => _repositorio.RemoveReunion(id);

        //Ocupar plaza (id, correo) (if correo != alumno...)
    }
}
