using System;
using System.Reflection;

namespace Ibanez_Molero_Daniel_Apuntate.Areas.HelpPage.ModelDescriptions
{
    public interface IModelDocumentationProvider
    {
        string GetDocumentation(MemberInfo member);

        string GetDocumentation(Type type);
    }
}