using System.Runtime.Serialization;

namespace Ibanez_Molero_Daniel_Apuntate.Models
{
    public class Correo
    {
        [DataMember(Name = "direccion")]
        public string Direccion { get; set; }
    }
}