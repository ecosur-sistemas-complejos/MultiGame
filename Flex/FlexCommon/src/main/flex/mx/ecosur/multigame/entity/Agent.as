/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 6/8/11
 * Time: 5:27 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.multigame.entity {

    [RemoteClass (alias="mx.ecosur.multigame.model.interfaces.Agent")]
    public class Agent extends GamePlayer {

        private var _ready:Boolean;

        public function Agent() {
            super();
            _ready = false;
        }

        public function get ready():Boolean {
            return _ready;
        }

        public function set ready(value:Boolean):void {
            _ready = value;
        }
    }
}
