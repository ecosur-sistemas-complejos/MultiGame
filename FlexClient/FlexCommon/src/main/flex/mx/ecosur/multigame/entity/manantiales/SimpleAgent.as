//copyright

package mx.ecosur.multigame.entity.manantiales {

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.manantiales.SimpleAgent")]
    public class SimpleAgent extends ManantialesPlayer {

        public var _type:String

        public function SimpleAgent() {
            super();
        }

        public function get type ():String {
            return _type;
        }

        public function set type (type:String):void {
            _type = type;
        }

    }
}