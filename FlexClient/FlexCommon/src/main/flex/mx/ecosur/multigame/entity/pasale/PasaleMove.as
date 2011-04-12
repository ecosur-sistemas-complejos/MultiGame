package mx.ecosur.multigame.entity.pasale
{
    import mx.ecosur.multigame.entity.Move;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.pasale.PasaleMove")]
    public class PasaleMove extends Move

    {
        private var _type:String;

        private var _replacementType:String;

        private var _badYear:Boolean;

        public function PasaleMove() {
            super();
        }

        public function get type ():String {
            return _type;
        }

        public function set type (type:String):void {
            _type = type;
        }

        public function get replacementType ():String {
            return _replacementType;
        }

        public function set replacementType(replacementType:String):void {
            _replacementType = replacementType;
        }

        public function get badYear ():Boolean {
            return _badYear;
        }

        public function set badYear (year:Boolean):void {
            _badYear = year;
        }


    }
}