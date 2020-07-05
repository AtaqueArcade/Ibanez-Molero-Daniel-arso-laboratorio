using Ibanez_Molero_Daniel_Apuntate.Models;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Bson;

namespace Ibanez_Molero_Daniel_Apuntate.Controllers
{
    [Route("api/[controller]/reuniones")]
    [ApiController]
    public class RestController : ControllerBase
    {
        private ReunionController _controller = new ReunionController();

        [HttpPost]
        public string CreateReunion([FromBody] Reunion reunion) => _controller.CreateReunion(reunion);

        [HttpGet("{id}")]
        public string GetReunion(string id) => _controller.GetReunion(id).ToJson();

        [HttpGet]
        public string GetAllReuniones() => _controller.GetAllReuniones();

        [HttpDelete("{id}")]
        public void Delete(string id, [FromBody] Correo correo) => _controller.RemoveReunion(id,correo.Direccion);

        [HttpPost("{id}")]
        public void JoinReunion(string id, [FromBody] Correo correo) => _controller.OcuparPlaza(id,correo.Direccion);
    }
}
