    /*
    * Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
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
    import flash.filters.GradientBevelFilter;
    import flash.media.Sound;
    import flash.media.SoundChannel;

    import mx.core.IFlexDisplayObject;
    import mx.core.UIComponent;
    import mx.ecosur.multigame.entity.Cell;
    import mx.ecosur.multigame.enum.Color;
    import mx.effects.Fade;
    import mx.effects.Sequence;

    /**
     * Visual class representing a token on the board. Contains an internal
     * cell object that represents a server side Cell.
     *
     *
     * Extra Licensing information:
     *
     * Sound file downloaded from "freesound.org", provided by user "vitriolix":
     *
     * http://www.freesound.org/samplesViewSingle.php?id=706
     *
     * Converted from .wav format for embedding by awaterma.  Used under the CreativeCommons
     * Sample Plus license:
     *
     * http://creativecommons.org/licenses/sampling+/1.0/
     *
     * The sound has been sampled and reused as an integral part of the grid game implementations
     * in this work.
     *
     */
    public class Token extends UIComponent{

        [Embed(source="/assets/706__vitriolix__pop.mp3")]
        protected var _sndCls:Class;
        protected var _snd:Sound;
        protected var _sndChannel:SoundChannel;

        protected var _cell:Cell;
        protected var _bg:Shape;
        protected var _selected:Boolean;
        protected var _deselectedFilters:Array;
        protected var _selectedFilters:Array;
        protected var _blinkAnim:Sequence;

        //flags
        protected var _bgDirty:Boolean;

        // Constants
        public static const BORDER_THICKNESS:Number = 1;

        /**
         * Default constructor. Initializes token with DEFAULT_ALPHA
         *
         */
        public function Token(){
            super();
            _snd = new _sndCls() as Sound;
            _selected = false;
            _bgDirty = false;
            _cell = null;

            // Create animation sequence
            _blinkAnim = new Sequence();
            var fadeOut:Fade = new Fade(this);
            fadeOut.alphaFrom = 1;
            fadeOut.alphaTo = 0.2;
            fadeOut.duration = 500;
            var fadeIn:Fade = new Fade(this);
            fadeIn.alphaFrom = 0.2;
            fadeIn.alphaTo = 1;
            fadeIn.duration = 500;
            _blinkAnim.addChild(fadeOut);
            _blinkAnim.addChild(fadeIn);
            _blinkAnim.repeatDelay = 300;
        }

        /* Getters and setters */

        public function get cell():Cell{
            return _cell;
        }

        public function set cell(cell:Cell):void {
            if (_cell == null || _cell.colorCode != cell.colorCode){

                //initialize filters
                _deselectedFilters = new Array();
                _selectedFilters = new Array();

                var glow:GlowFilter = new GlowFilter();
                glow.color = 0xffffff;
                glow.alpha = 1;
                glow.blurX = 5;
                glow.blurY = 5;
                glow.inner = false;
                glow.quality = BitmapFilterQuality.MEDIUM;
                _selectedFilters.push(glow);

                var gradientBevel:GradientBevelFilter = new GradientBevelFilter();
                gradientBevel.distance = 6;
                gradientBevel.angle = 225; // opposite of 45 degrees
                gradientBevel.colors = [Color.findIntermediateColor(
                    0xffffff, cell.colorCode, 0.5), cell.colorCode, Color.findIntermediateColor(
                    0x000000, cell.colorCode, 0.5)];
                gradientBevel.alphas = [1, 0, 1];
                gradientBevel.ratios = [0, 128, 255];
                gradientBevel.blurX = 6;
                gradientBevel.blurY = 6;
                gradientBevel.quality = BitmapFilterQuality.HIGH;
                _deselectedFilters.push(gradientBevel);
                _selectedFilters.push(gradientBevel);

                _bgDirty = true;
                invalidateDisplayList();
            }
            
            _cell = cell;
        }

        public function set selected(selected:Boolean):void{
            if (_selected != selected){
                _selected = selected;
                invalidateDisplayList();
            }
        }

        /**
         * Blinks the token to catch the users attention
         *
         * @param repeat the number of blinks to execute. If this parameter is less
         * than 1 or not specified the blink is continuous until stopBlink is called.
         *
         */
        public function blink(repeat:int = 0):void{
            if(_blinkAnim.isPlaying){
                _blinkAnim.stop();
            }
            _blinkAnim.repeatCount = repeat;
            _blinkAnim.play();
        }

        public function stopBlink():void{
            if(_blinkAnim.isPlaying){
                _blinkAnim.stop();
            }
            this.alpha = 1;
        }

        /**
         * Creates a display object that is visually the same as the Token
         * to be used as a drag image.
         *
         * @return the drag image
         */
        public function createDragImage():IFlexDisplayObject{
            var dragImage:Token = new Token();
            dragImage.cell = _cell;
            dragImage.width = width;
            dragImage.height = height;
            return IFlexDisplayObject(dragImage);
        }

        public function play():void {
            _sndChannel=_snd.play();
        }

        /**
         * Creates a clone of the token copying the internal cell data
         *
         * @return the clone
         */
        public function clone():Token{
            var clone:Token = new Token();
            clone.cell = _cell;
            return clone;
        }

        /* Overrides */

        override protected function createChildren():void{
            _bg = new Shape();
            addChild(_bg);
        }

        override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void {

            // Do nothing if color not set
            if (_cell == null || _cell.color == null){
                return;
            }

            // Redraw background
            _bg.graphics.clear();
            _bg.x = - unscaledWidth / 2;
            _bg.y = - unscaledHeight / 2;
            _bg.graphics.beginFill(_cell.colorCode, 1);
            _bg.graphics.lineStyle(BORDER_THICKNESS, _cell.colorCode, 1);
            _bg.graphics.drawCircle(unscaledWidth/2, unscaledHeight/2, unscaledWidth / 2);
            _bg.graphics.endFill();
            _bgDirty = false;

            // Set filters acording to whether the token is selected or not
            if (_selected){
                _bg.filters = _selectedFilters;
            }else{
                _bg.filters = _deselectedFilters;
            }
        }

        override public function toString():String{
            return "cell = {" + cell + "}";
        }
    }
}