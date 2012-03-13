/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/7/12
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile.component {

import mx.ecosur.multigame.enum.Color;

import spark.components.TextArea;
    import spark.components.supportClasses.SkinnableComponent;

    public class StatusBox extends SkinnableComponent {

        [Bindable]
        [SkinPart(required="true")]
        public var textField:TextArea;

        private var _color:uint;


        public function StatusBox() {
            super();
        }


        [Bindable]
        public function get color():uint {
            return _color;
        }

        public function set color(value:uint):void {
            _color = value;
            invalidateSkinState();
        }

        public function showMessage(msg:String):void {
            this.textField.text = msg;
            invalidateSkinState();
        }
    }
}
