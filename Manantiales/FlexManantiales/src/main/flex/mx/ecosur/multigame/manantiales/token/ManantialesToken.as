package mx.ecosur.multigame.manantiales.token
{
    import mx.controls.Image;
    import mx.controls.Label;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.entity.Cell;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.entity.manantiales.Ficha;
    import mx.ecosur.multigame.enum.manantiales.TokenType;
    import mx.resources.IResourceManager;
    import mx.resources.ResourceManager;

public class ManantialesToken extends Token
    {
        protected var _label:String;
        protected var _tooltip:String;
        protected var _colorCode:uint;
        protected var _bgImage:Image;
        protected var _territoryColor:Color;
        protected var _txt:Label;
        protected var _ficha:Ficha;
        protected var _type:String;
        protected var _placed:Boolean;
    
        public function ManantialesToken()
        {
            super();
        }

        public function get ficha ():Ficha {
            return _ficha;
        }

        public function set ficha(ficha:Ficha):void {
            _ficha = ficha;
            cell = ficha;
        }
        
        public function get type ():String {
            return _type;
        }

        override public function set cell (cell:Cell):void{
          if (_type != null) {
              _ficha = new Ficha();
              _ficha.row = cell.row;
              _ficha.column = cell.column;
              _ficha.color = cell.color;
              _ficha.type = _type;
          }

          super.cell = cell;

        }

        public function get territoryColor():Color {
            return this._territoryColor;
        }

        public function get colorCode():uint {
            return _cell.colorCode;
        }

        public function get txt():Label {
            return _txt;
        }

        override protected function createChildren():void{
            super.createChildren();

            if (_label != null) {
                _txt = new Label();
                _txt.text = _label;
                _txt.styleName = "manantialesToken";
                addChild(_txt);
            }

            if (_tooltip != null)
                this.toolTip = _tooltip;
        }

        override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void
        {
            // Do nothing if color not set
            if (_cell == null || _cell.color == null){
                return;
            }

            // Redraw background
            _bg.graphics.clear();
            _bg.x = - unscaledWidth / 2;
            _bg.y = - unscaledHeight / 2;
            _bg.graphics.beginFill(colorCode, 1);
            _bg.graphics.lineStyle(BORDER_THICKNESS, colorCode, 1);
            _bg.graphics.drawCircle(unscaledWidth/2, unscaledHeight/2, unscaledWidth / 2);
            _bg.graphics.endFill();
            _bgDirty = false;

            // Position label
            if (_txt != null && _txt.getExplicitOrMeasuredWidth() && _txt.getExplicitOrMeasuredHeight()) {
                var scale:Number = Math.min(unscaledWidth / _txt.getExplicitOrMeasuredWidth(), unscaledHeight / _txt.getExplicitOrMeasuredHeight());
                _txt.scaleX = scale;
                _txt.scaleY = scale;
                _txt.setActualSize(_txt.getExplicitOrMeasuredWidth(), _txt.getExplicitOrMeasuredHeight());
                _txt.x = - unscaledWidth / 2;
                _txt.y = - unscaledHeight / 2;
            }

            // Set filters acording to whether the token is selected or not
            if (_selected){
                _bg.filters = _selectedFilters;
            }else{
                _bg.filters = _deselectedFilters;
            }
        }
        /**
         * Creates a display object that is visually the same as the Token
         * to be used as a drag image.
         *
         * @return the drag image
         */
        override public function createDragImage():IFlexDisplayObject{
            var token:ManantialesToken;
            switch (_type) {
                case TokenType.FOREST:
                   token = new ForestToken();
                   token.ficha = ficha;
                   break;
                case TokenType.MODERATE:
                   token = new ModerateToken();
                   token.ficha = ficha;
                   break;
                case TokenType.INTENSIVE:
                   token = new IntensiveToken();
                   token.ficha = ficha;
                   break;
                case TokenType.VIVERO:
                   token = new ViveroToken();
                   token.ficha = ficha;
                   break;
                case TokenType.SILVOPASTORAL:
                   token = new SilvopastoralToken();
                   token.ficha = ficha;
                   break;
                default:
                    break;
            }
            token.width = width;
            token.height = height;
            return IFlexDisplayObject(token);
        }

        public function get placed():Boolean {
            return _placed;
        }

        public function set placed(value:Boolean):void {
            _placed = value;
        }
    }
}
