using System.Collections.Generic;
using MongoDB.Bson;
using MongoDB.Driver;

namespace Ibanez_Molero_Daniel_Apuntate.Repositories
{
    public class RepositorioReuniones
    {
        private IMongoCollection<BsonDocument> reuniones;
        // La lista de espera ha de ser igual de persistente que las reuniones guardadas
        // Si no que el sistema cayese implicaria la perdida de esta
        private IMongoCollection<BsonDocument> listaEspera;

        public RepositorioReuniones()
        {
            var client =
                new MongoClient(
                    "mongodb+srv://ataquearcade:huWAN4jGusPRjqV@arso-vaorn.mongodb.net/ArSo?retryWrites=true&w=majority");
            var database = client.GetDatabase("ArSo");
            reuniones = database.GetCollection<BsonDocument>("reuniones");
            listaEspera = database.GetCollection<BsonDocument>("listaEspera");
        }

        // reuniones
        public string SaveReunion(BsonDocument reunion)
        {
            reuniones.InsertOne(reunion);
            return reunion["_id"].ToString();
        }

        public void UpdateReunion(ObjectId id, BsonDocument reunion)
        {
            var filter = Builders<BsonDocument>.Filter.Eq("_id", id);
            reuniones.ReplaceOneAsync(filter, reunion);
        }

        public BsonDocument GetReunion(ObjectId id) =>
            reuniones.Find(Builders<BsonDocument>.Filter.Eq("_id", id)).FirstOrDefault();

        public bool RemoveReunion(ObjectId id) =>
            reuniones.DeleteOne(Builders<BsonDocument>.Filter.Eq("_id", id)).DeletedCount > 0;

        public List<BsonDocument> GetAll() => reuniones.Find(f => true).ToList();

        // lista de espera
        public string SaveEspera(BsonDocument espera)
        {
            listaEspera.InsertOne(espera);
            return espera["_id"].ToString();
        }

        public BsonDocument GetEspera(string reunion, int grupo)
        {
            var query = Builders<BsonDocument>.Filter.And(
                Builders<BsonDocument>.Filter.Eq("reunion", reunion),
                Builders<BsonDocument>.Filter.Eq("grupo", grupo)
            );
            return listaEspera.Find(query).FirstOrDefault();
        }

        public bool RemoveEspera(string correo, string reunion)
        {
            var query = Builders<BsonDocument>.Filter.And(
                Builders<BsonDocument>.Filter.Eq("correo", correo),
                Builders<BsonDocument>.Filter.Eq("reunion", reunion)
            );
            return listaEspera.DeleteMany(query).DeletedCount > 0;
        }

        public bool IsEspera(string correo, string reunion)
        {
            var query = Builders<BsonDocument>.Filter.And(
                Builders<BsonDocument>.Filter.Eq("correo", correo),
                Builders<BsonDocument>.Filter.Eq("reunion", reunion)
            );
            return listaEspera.Find(query).FirstOrDefault() != null;
        }
    }
}
