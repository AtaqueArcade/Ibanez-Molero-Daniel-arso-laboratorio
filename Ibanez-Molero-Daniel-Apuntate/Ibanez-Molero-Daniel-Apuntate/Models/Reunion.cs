using System;
using System.Runtime.Serialization;

namespace Ibanez_Molero_Daniel_Apuntate.Models
{
    [DataContract]
    public class Reunion
    {
        [DataMember(Name = "titulo")]
        public string Titulo { get; set; }

        [DataMember(Name = "organizador")]
        public string Organizador { get; set; }

        [DataMember(Name = "ubicacion")]
        public string Ubicacion { get; set; }

        [DataMember(Name = "categoria")]
        public string Categoria { get; set; }

        [DataMember(Name = "descripcion")]
        public string Descripcion { get; set; }

        [DataMember(Name = "inicio")]
        public DateTime Inicio { get; set; }

        [DataMember(Name = "fin")]
        public DateTime Fin { get; set; }

        [DataMember(Name = "frecuencia")]
        public string Frecuencia { get; set; }

        [DataMember(Name = "apertura")]
        public DateTime Apertura { get; set; }

        [DataMember(Name = "cierre")]
        public DateTime Cierre { get; set; }

        [DataMember(Name = "componentes")]
        public int Componentes { get; set; }

        [DataMember(Name = "intervalos")]
        public int Intervalos { get; set; }

        [DataMember(Name = "tipo")]
        public string Tipo { get; set; }
    }
}