package mx.ecosur.multigame.pasale.entity
{
    import mx.ecosur.multigame.entity.Cell;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.pasale.Ficha")]
    public class Territory extends Cell
    {
        protected var _type:String;

        public function Territory()
        {
          super();
        }

        public function get type():String {
           return this._type;
        }

        public function set type (type:String):void {
            this._type = type;
        }

        /**
         * Returns a clone of the actual ficha. The clone
         * is not recursive.
         *
         * @return the cloned cell
         */
        override public function clone():Cell{
            var clone:Territory = new Territory();
            clone.color = _color;
            clone.column = _column;
            clone.row = _row;
            clone.type = _type;
            return clone;
        }
    }
}