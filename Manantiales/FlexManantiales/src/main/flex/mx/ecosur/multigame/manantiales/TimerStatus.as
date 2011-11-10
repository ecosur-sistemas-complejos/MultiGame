/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 11/10/11
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.multigame.manantiales {

    import mx.ecosur.multigame.component.GameStatus;

    public class TimerStatus extends GameStatus {

        public function TimerStatus(){
                                         super();
                                         }

        override public function showMessage(message:String, color:uint):void {
            if (!_active)
                return;
            _message = message;
            _txtField.text = _message;
            _color = color;
            invalidateSize();
            invalidateDisplayList();
        }
    }
}
