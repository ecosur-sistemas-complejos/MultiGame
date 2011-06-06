/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com, awaterma@ecosur.mx
*/

package mx.ecosur.multigame.entity {
    import flash.media.Sound;
    import flash.media.SoundChannel;

    /**
     * Represents a server side GamePlayer object.
     *
     * Extra Licensing information:
     *
     * Sound file downloaded from "freesound.org", provided by user "acclivity":
     *
     * http://www.freesound.org/samplesViewSingle.php?id=30601
     *
     * Converted from .wav format to mp3 for embedding by awaterma.  Used under the
     * CreativeCommons Sample Plus license:
     *
     * http://creativecommons.org/licenses/sampling+/1.0/
     *
     * The sound has been sampled and reused as an integral part of the grid game implementations
     * in this work.
     */
    [RemoteClass (alias="mx.ecosur.multigame.impl.model.GridPlayer")]
    public class GamePlayer {

        private var _id:int;
        private var _color:String;
        private var _turn:Boolean;
        private var _name:String;

        [Embed(source="/assets/30601__acclivity__Goblet_F_Soft.mp3")]
        protected var _sndCls:Class;
        protected var _snd:Sound;
        protected var _sndChannel:SoundChannel;

        public function GamePlayer(){
            super();
            _snd = new _sndCls() as Sound;
        }

        public function get id():int{
            return _id;
        }

        public function set id(id:int):void {
            _id = id;
        }

        public function get name():String {
            return _name;
        }

        public function set name(value:String):void {
            _name = value;
        }

        public function get color():String{
            return _color;
        }

        public function set color(color:String):void {
            _color = color;
        }

        public function get turn():Boolean{
            return _turn;
        }

        public function set turn(turn:Boolean):void {
            _turn = turn;
        }

        public function toString():String{
            return "id = " + id + ", player = {" + name + "}, color = " +
                 color + ", turn = " + turn;
        }

        public function play():void {
             _sndChannel=_snd.play();
        }
    }
}
