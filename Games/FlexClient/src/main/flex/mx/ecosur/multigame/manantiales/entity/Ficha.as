//copyright

package mx.ecosur.multigame.manantiales.entity
{
    import mx.controls.PopUpButton;
    import mx.ecosur.multigame.entity.Cell;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.resources.ResourceManager;

[RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.manantiales.ManantialesFicha")]
    public class Ficha extends Cell
    {
        protected var _type:String;

        public function Ficha()
        {
            super();
        }

        public function get type():String {
            return this._type;
        }

        public function set type (type:String):void {
            this._type = type;
        }

        public function get typeName():String {
            var ret:String;

            switch (_type) {
                case TokenType.FOREST:
                    ret = ResourceManager.getInstance().getString("StringsBundle", "manantiales.token.forest");
                    break;
                case TokenType.MODERATE:
                    ret = ResourceManager.getInstance().getString("StringsBundle", "manantiales.token.moderate");
                    break;
                case TokenType.INTENSIVE:
                    ret = ResourceManager.getInstance().getString("StringsBundle", "manantiales.token.intensive");
                    break;
                case TokenType.VIVERO:
                    ret = ResourceManager.getInstance().getString("StringsBundle", "manantiales.token.vivero");
                    break;
                case TokenType.SILVOPASTORAL:
                    ret = ResourceManager.getInstance().getString("StringsBundle", "manantiales.token.silvopastoral");
                    break;
                default:
                    ret = "UNKNOWN!";
            }

            return ret;
        }

        /**
         * Returns a clone of the actual ficha. The clone
         * is not recursive.
         * 
         * @return the cloned cell
         */
        override public function clone():Cell{
            var clone:Ficha = new Ficha();
            clone.color = _color;
            clone.column = _column;
            clone.row = _row;
            clone.type = _type;
            return clone; 
        }
    }
}