using System.Runtime.Serialization;

namespace Ibanez_Molero_Daniel_Apuntate.Models
{
    //Clase envoltorio para las peticiones
    public class Correo
    {
        [DataMember(Name = "direccion")]
        public string Direccion { get; set; }
    }
}