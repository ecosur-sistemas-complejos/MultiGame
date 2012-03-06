/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/5/12
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile {


    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    public class Controller {

        public function Controller() {
        }

        public function dragAndDropResultHandler (event:ResultEvent):void {
            trace(event.result);

        }

        public function dragAndDropFaultHandler (event:FaultEvent):void {
            trace(event.fault);

        }

    }
}
