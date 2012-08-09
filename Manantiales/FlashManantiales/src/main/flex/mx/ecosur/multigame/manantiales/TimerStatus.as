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

        private var _maximum:Number;

        private var _current:Number;

        public function TimerStatus(){
            super();
        }

        public function displayTime(color:uint):void {
            _current += 1000;
            if (!_active)
                return;
            _txtField.text = currentTime();
            _color = color;
            invalidateSize();
            invalidateDisplayList();
            if (_current >= _maximum)
                flashMessage();
        }

        public function get maximum():Number {
            return _maximum;
        }

        public function set maximum(value:Number):void {
            _maximum = value;
        }

        public function get current():Number {
            return _current;
        }

        public function set current(value:Number):void {
            _current = value;
        }

        private function currentTime ():String {
            var ret:String;
            var minutes:int, seconds:Number;
            seconds = Math.floor(_current/1000);
            if(seconds > 59) {
                minutes = Math.floor(seconds / 60);
                if (minutes >= 45)
                    ret = "45:00";
                else if (seconds %  60 > 9)
                    ret = String(minutes) + ":" + String(seconds % 60);
                else
                    ret = String(minutes) + ":0" + String(seconds % 60);
            } else {
                if (seconds > 9)
                    ret = "0:" + String(Math.floor(seconds));
                else
                    ret = "0:0" + String(Math.floor(seconds));
            }

            return ret;
       }
    }
}
