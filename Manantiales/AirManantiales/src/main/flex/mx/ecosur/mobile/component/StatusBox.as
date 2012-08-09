/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 3/7/12
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.mobile.component {

    import mx.ecosur.multigame.enum.Color;
import mx.effects.Sequence;

import spark.components.TextArea;
    import spark.components.supportClasses.SkinnableComponent;
import spark.effects.Fade;
import spark.effects.SetAction;

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
        }

        public function flashMessage():void {
            /* Fade out then in */
            var out:Fade = new Fade();
            out.target = this;
            out.alphaFrom = 1.0;
            out.alphaTo = 0;
            out.duration = 350;

            var fin:Fade = new Fade();
            fin.target = this;
            fin.alphaFrom = 0;
            fin.alphaTo = 1.0;
            fin.duration =  350;

            var seq:Sequence = new Sequence();
            seq.addChild(out);
            seq.addChild(fin);
            seq.play();
        }

    }
}
