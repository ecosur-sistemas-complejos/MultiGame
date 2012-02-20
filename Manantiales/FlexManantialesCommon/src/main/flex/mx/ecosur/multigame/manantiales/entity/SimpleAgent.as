//copyright

package mx.ecosur.multigame.manantiales.entity {

    [RemoteClass (alias=
        "mx.ecosur.multigame.manantiales.entity.SimpleAgent")]
    public class SimpleAgent extends ManantialesPlayer {

        public var _type:String

        private var _nextMove:ManantialesMove;

        public function SimpleAgent() {
            super();
        }

        public function get nextMove():ManantialesMove {
            return _nextMove;
        }

        public function set nextMove(value:ManantialesMove):void {
            _nextMove = value;
        }

        public function get type ():String {
            return _type;
        }

        public function set type (type:String):void {
            _type = type;
        }

    }
}
