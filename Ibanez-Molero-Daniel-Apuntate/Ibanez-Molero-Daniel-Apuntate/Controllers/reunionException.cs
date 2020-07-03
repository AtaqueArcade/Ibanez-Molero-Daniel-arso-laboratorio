using System;

namespace Ibanez_Molero_Daniel_Apuntate.Controllers
{
    public class ReunionException : Exception
    {
        public ReunionException(string msg)
            : base(String.Format("Error: {0}", msg))
        {
        }
    }
}