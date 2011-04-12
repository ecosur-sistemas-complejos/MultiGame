package mx.ecosur.multigame.pasale {

    import as3isolib.display.primitive.IsoBox;
    import mx.ecosur.multigame.pasale.enum.UseType;

    public class PasaleBox extends IsoBox {

        private var _type:String;

        private var _row:int;
        private var _column:int;

        public function PasaleBox() {
            super();
        }

        public function get type():String {
            return _type;
        }

        public function set type(value:String):void {
            _type = value;
        }

        public function get row():int {
            return _row;
        }

        public function set row(value:int):void {
            _row = value;
        }

        public function get column():int {
            return _column;
        }

        public function set column(value:int):void {
            _column = value;
        }
    }
}