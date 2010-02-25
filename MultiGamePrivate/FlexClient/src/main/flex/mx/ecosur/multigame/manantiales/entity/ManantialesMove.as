package mx.ecosur.multigame.manantiales.entity
{
    import mx.ecosur.multigame.entity.Move;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.manantiales.ManantialesMove")]
    public class ManantialesMove extends Move
    {
        private var _badYear:Boolean;

        private var _mode:String;

        public function ManantialesMove()
        {
            super();
            _badYear = false;
        }

        public function get badYear():Boolean {
            return _badYear;
        }

        public function set badYear (year:Boolean):void {
            _badYear = year;
        }

        public function get mode ():String {
            return _mode;
        }

        public function set mode (mode:String):void {
            _mode = mode;
        }

        public override function toString():String {
            return super.toString() + ", badYear [" + _badYear + "], mode [ " + _mode.toString() + "]";
        }

    }
}