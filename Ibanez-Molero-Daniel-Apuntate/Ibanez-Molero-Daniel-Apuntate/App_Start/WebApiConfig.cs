using System;
using System.Collections.Generic;
using System.Linq;
using System.Web.Http;

namespace Ibanez_Molero_Daniel_Apuntate
{
    public static class WebApiConfig
    {
        public static void Register(HttpConfiguration config)
        {
            // Configuración y servicios de API web

            // Rutas de API web
            config.MapHttpAttributeRoutes();

            config.Routes.MapHttpRoute(
                name: "DefaultApi",
                routeTemplate: "api/{controller}/reuniones/{id}",
                defaults: new { id = RouteParameter.Optional }
            );
        }
    }
}
