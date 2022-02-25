using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Linfinitype
{
    abstract class Application
    {
        public string Name = "";
        public abstract void Start();
        public abstract void Unregister();
    }
}
