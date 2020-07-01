using System.Web;
using System.Web.Mvc;

namespace Ibanez_Molero_Daniel_Apuntate
{
    public class FilterConfig
    {
        public static void RegisterGlobalFilters(GlobalFilterCollection filters)
        {
            filters.Add(new HandleErrorAttribute());
        }
    }
}
