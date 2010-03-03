    /*
    * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
    *
    * Licensed under the Academic Free License v. 3.2.
    * http://www.opensource.org/licenses/afl-3.0.php
    */

    /**
    * @author max@alwayssunny.com
    */

    package mx.ecosur.multigame.component {

    import flash.display.Shape;
    import flash.filters.BitmapFilterQuality;
    import flash.filters.GlowFilter;

    import mx.controls.Image;
    import mx.core.UIComponent;

    [Style(name="padding", type="Number", format="Length")]

    /**
     * Represents a cell on the board.
     */
    public class BoardCell extends UIComponent {

        protected var _bg:Shape;
        protected var _token:Token;
        protected var _row:int;
        protected var _column:int;
        protected var _bgImage:Image;

        /* colors */
        protected var _bgColor:uint;
        protected var _borderColor:uint;
        protected var _borderThickness:Number;

        /* default colors */
        protected var _defaultBgColor:uint;
        protected var _defaultBorderColor:uint;
        protected var _defaultBorderThickness:Number;

        /* selected move colors */
        protected var _selectedBgColor:uint;
        protected var _selectedBorderColor:uint;
        protected var _selectedBorderThickness:Number;

        /* filters for background */
        protected var _bgFilters:Array;
        protected var _bgDefaultFilters:Array;
        protected var _bgSelectedFilters:Array;

        /**
         * Constructor
         *
         * @param row the row number, 0 indicates the top row
         * @param column the column number, 0 indicates the left most column
         * @param bgColor a hexidecimal representation of the background color
         * @param borderColor a hexidecimal representation of the the border color
         * @param borderThickness the thickness, in pixels, of the border
         *
         */
        public function BoardCell(column:int, row:int, bgColor:uint, borderColor:uint, borderThickness:Number){
            super();

            //define position in board
            _row = row;
            _column = column;

            //define colors
            _bgColor = bgColor;
            _borderColor = borderColor;
            _borderThickness = borderThickness;
            _defaultBgColor = bgColor;
            _defaultBorderColor = borderColor;
            _defaultBorderThickness = borderThickness;
            _selectedBgColor = bgColor;
            _selectedBorderColor = borderColor;
            _selectedBorderThickness = 5;

            //initialize filters
            _bgDefaultFilters = new Array();
            _bgSelectedFilters = new Array();
            var glow:GlowFilter = new GlowFilter();
            glow.color = 0xffffff;
            glow.alpha = 1;
            glow.blurX = 10;
            glow.blurY = 10;
            glow.inner = true;
            glow.quality = BitmapFilterQuality.MEDIUM;
            _bgSelectedFilters.push(glow);

        }

        /* Getters and setters */

        public function get token():Token{
            return _token;
        }

        public function set token(token:Token):void{
            if (_token != null){
                removeChild(_token);
            }

            if (token != null){
                addChild(token);
                invalidateDisplayList();
            }
            _token = token;
        }

        public function get row():int{
            return _row;
        }

        public function get column():int{
            return _column;
        }

        public function set bgImage(bgImage:Image):void{
            _bgImage = bgImage;
            addChild(_bgImage);
        }

        public function get bgImage():Image{
            return _bgImage
        }

        /**
         * Visualy selects the board cell.
         *
         * @param color the selected color
         */
        public function select(color:uint):void{

            /* change filters */
            var glow:GlowFilter = GlowFilter(_bgSelectedFilters[0]);
            glow.color = color;
            _bgFilters = _bgSelectedFilters;
            _bg.filters = _bgFilters;

        }

        /**
         * Visualy resets the board cell after selection.
         *
         */
        public function reset():void{

            /* change filters */
            _bgFilters = _bgDefaultFilters;
            _bg.filters = _bgFilters;
            invalidateDisplayList();
        }

        /* Overrides */

        override protected function createChildren():void{
            _bg = new Shape();
            addChild(_bg);
        }

        override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void {

            super.updateDisplayList(unscaledWidth, unscaledHeight);

            //redraw bg
            _bg.graphics.clear();
            _bg.graphics.beginFill(_bgColor, 1);
            if (_borderThickness > 0){
                _bg.graphics.lineStyle(_borderThickness, _borderColor, 1);
            }
            _bg.graphics.drawRect(0, 0, unscaledWidth, unscaledHeight);
            _bg.graphics.endFill();
            _bg.filters = _bgFilters;

            //center and resize background image if present
            if(_bgImage){
                _bgImage.x = unscaledWidth / 2;
                _bgImage.y = unscaledHeight / 2;
                _bgImage.width = unscaledWidth - 2 * getStyle("padding");
                _bgImage.height = unscaledHeight - 2 * getStyle("padding");
            }

            //define size of token acording the the size of this
            if (_token){
                _token.rotation = - parent.rotation;
                _token.width = unscaledWidth - 2 * getStyle("padding");
                _token.height = unscaledHeight - 2 * getStyle("padding");
                _token.x = unscaledWidth / 2;
                _token.y = unscaledHeight / 2;
            }
        }

    }
    }
