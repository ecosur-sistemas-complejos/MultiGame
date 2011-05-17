/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 5/16/11
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.multigame.pasale {
import mx.ecosur.multigame.pasale.entity.PasaleFicha;

[RemoteClass (alias="mx.ecosur.multigame.impl.util.pasale.MutationEvent")]
    public class MutationEvent {

        private var _ficha:PasaleFicha;

        private var _reason:String;

        public function MutationEvent() {

        }

        public function get ficha():PasaleFicha {
            return _ficha;
        }

        public function set ficha(value:PasaleFicha):void {
            _ficha = value;
        }

        public function get reason():String {
            return _reason;
        }

        public function set reason(value:String):void {
            _reason = value;
        }
    }
}
