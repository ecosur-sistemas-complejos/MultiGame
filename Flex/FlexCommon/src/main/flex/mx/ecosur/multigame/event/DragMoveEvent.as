/**
 * Contains information about a move event.
 *
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.event {

    import flash.events.Event;
    import mx.ecosur.multigame.entity.Move;

    public class DragMoveEvent extends Event {
        
        private var _move:Move;
        
        public function DragMoveEvent(type:String, move:Move, bubbles:Boolean=false, cancelable:Boolean=false) {
            super(type,  bubbles,  cancelable);
            _move = move;
        }


        public function get move():Move {
            return _move;
        }

        public function set move(value:Move):void {
            _move = value;
        }
    }
}
