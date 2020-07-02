using System.Runtime.Serialization;

namespace Ibanez_Molero_Daniel_Apuntate.Models
{
    public class Grupo
    {
        [DataMember(Name = "componentes")]
        public string Componentes{ get; set; }
        [DataMember(Name = "duracion")]
        public double Duracion{ get; set; }
    }
}