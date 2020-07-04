using System.Web.Http;
using Ibanez_Molero_Daniel_Apuntate.Models;
using MongoDB.Bson;

namespace Ibanez_Molero_Daniel_Apuntate.Controllers
{
    public class RestController : ApiController
    {
        private ReunionController _controller;

        public RestController()
        {
            _controller = new ReunionController();
        }

        // POST: api/Rest/reuniones/
        public string CreateReunion([FromBody] Reunion reunion) => _controller.CreateReunion(reunion);

        // GET: api/Rest/reuniones/{id}
        public string GetReunion([FromUri] string id) => _controller.GetReunion(id).ToJson();

        // GET: api/Rest/reuniones/
        public string GetAllReuniones() => _controller.GetAllReuniones();

        // DELETE: api/Rest/reuniones/{id}
        public void Delete([FromUri]string id) => _controller.RemoveReunion(id);

        // POST: api/Rest/reuniones/{id}
        public void JoinReunion([FromUri] string id, [FromBody] Correo correo) => _controller.OcuparPlaza(id,correo.Direccion);
    }
}
