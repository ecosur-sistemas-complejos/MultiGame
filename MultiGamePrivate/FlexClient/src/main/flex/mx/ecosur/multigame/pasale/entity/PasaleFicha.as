package mx.ecosur.multigame.pasale.entity {
    import mx.ecosur.multigame.entity.Cell;

    [RemoteClass (alias="mx.ecosur.multigame.impl.entity.pasale.PasaleFicha")]
    public class PasaleFicha extends Cell {

        private var _type:String;


        public function PasaleFicha() {
            super();
        }

        public function get type():String {
            return _type;
        }

        public function set type(type:String):void {
            _type = type;
        }
    }
}