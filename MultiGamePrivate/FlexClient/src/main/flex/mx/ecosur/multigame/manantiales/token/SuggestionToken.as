//copyright

package mx.ecosur.multigame.manantiales.token
{
    import flash.events.MouseEvent;

    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;
    import mx.ecosur.multigame.manantiales.enum.TokenType;

    public class SuggestionToken extends ManantialesToken {

        private var _suggestion:Suggestion;

        public function SuggestionToken (suggestion:Suggestion) {
            _suggestion = suggestion;
            var destination:Ficha = Ficha(suggestion.move.destinationCell);

            switch (destination.type) {
            case TokenType.FOREST:
                   _tooltip = "Suggestion (Managed Forest)";
                    _label = "F";
                   break;
                case TokenType.INTENSIVE:
                    _tooltip = "Suggestion (Intensive Grazing)";
                    _label = "I";
                   break;
                case TokenType.MODERATE:
                   _tooltip = "Suggestion (Moderate Grazing)";
                    _label = "M";
                   break;
                case TokenType.SILVOPASTORAL:
                   _tooltip = "Suggestion (SilvoPastoral)";
                    _label = "S";
                   break;
                case TokenType.VIVERO:
                   _tooltip = "Suggestion (Vivero)";
                    _label = "V";
                   break;
                default:
                   break;
            }

            alpha = .5;

         }

        override public function get colorCode():uint {
            var ret:uint = _cell.colorCode;

            switch (_suggestion.suggestor.color) {
                case Color.BLUE:
                    ret = 0x0000BB;
                    break;
                case Color.GREEN:
                    ret = 0x00BB00;
                    break;
                case Color.RED:
                    ret = 0xBB0000;
                    break;
                case Color.YELLOW:
                    ret = 0xFFCC00;
                    break;
                default:
                    break;
            }

            return ret;
        }

        override public function stopBlink():void{
            if(_blinkAnim.isPlaying){
                _blinkAnim.stop();
            }
            this.alpha = 0.5;
        }
    }
}
