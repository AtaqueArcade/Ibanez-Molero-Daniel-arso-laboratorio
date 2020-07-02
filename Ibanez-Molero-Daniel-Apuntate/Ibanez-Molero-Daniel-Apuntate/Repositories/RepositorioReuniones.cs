using System.Collections.Generic;
using MongoDB.Bson;
using MongoDB.Driver;

namespace Ibanez_Molero_Daniel_Apuntate.Repositories
{
    public class RepositorioReuniones
    {
        private IMongoCollection<BsonDocument> reuniones;

        public RepositorioReuniones()
        {
            var client =
                new MongoClient(
                    "mongodb+srv://ataquearcade:huWAN4jGusPRjqV@arso-vaorn.mongodb.net/ArSo?retryWrites=true&w=majority");
            var database = client.GetDatabase("ArSo");
            reuniones = database.GetCollection<BsonDocument>("reuniones");
        }

        public string SaveReunion(BsonDocument reunion)
        {
            reuniones.InsertOne(reunion);
            return reunion["_id"].ToString();
        }

        public void UpdateReunion(string id, BsonDocument reunion)
        {
            var filter = Builders<BsonDocument>.Filter.Eq("_id", id);
            reuniones.UpdateOne(filter, reunion);
        }

        public BsonDocument GetReunion(string id) =>
            reuniones.Find(Builders<BsonDocument>.Filter.Eq("_id", id)).FirstOrDefault();

        public bool RemoveReunion(string id) =>
            reuniones.DeleteOne(Builders<BsonDocument>.Filter.Eq("_id", id)).DeletedCount > 0;

        public List<BsonDocument> GetAll() => reuniones.Find(f => true).ToList();

        public void ResetReuniones() =>
            reuniones.DeleteMany(f => true);
    }
}
