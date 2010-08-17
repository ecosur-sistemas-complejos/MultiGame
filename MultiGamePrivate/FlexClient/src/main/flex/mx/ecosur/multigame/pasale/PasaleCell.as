package mx.ecosur.multigame.pasale {

    import as3isolib.display.primitive.IsoBox;
    import mx.ecosur.multigame.pasale.enum.UseType;

    public class PasaleCell extends IsoBox {

        private var _type:UseType;

        private var _row:int;
        private var _column:int;

        public function PasaleCell() {
            super();
        }

        public function get type():UseType {
            return _type;
        }

        public function set type(value:UseType):void {
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