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
import flash.events.MouseEvent;

import mx.core.UIComponent;
import mx.ecosur.multigame.entity.Cell;
import mx.ecosur.multigame.entity.GamePlayer;
import mx.effects.AnimateProperty;
import mx.events.DragEvent;
import mx.events.EffectEvent;

public class TokenStore extends UIComponent {

    protected static const INITIAL_N_TOKENS:int = 50;

    protected var _bg:Shape;
    protected var _tokenSize:Number;
    protected var _nTokens:Number;
    protected var _currentPlayer:GamePlayer
    protected var _active:Boolean;
    protected var _tokensPerRow:Number;

    protected var _startMoveHandler:Function;
    protected var _endMoveHandler:Function;

    public static const START_MOVE_EVENT:String = "startMove";
    public static const END_MOVE_EVENT:String = "endMove";
    public static const OVERLAP:Number = 0.7;
    public static const PADDING:Number = 5;
    public static const CORNER_SIZE:Number = 15;

    [Event (name = START_MOVE_EVENT, type="mx.events.DynamicEvent")]
    [Event (name = END_MOVE_EVENT, type="mx.events.DynamicEvent")]

    public function TokenStore() {
        super();
        _nTokens = 0;
    }

    public function set tokenSize(tokenSize:Number):void{
        if (_tokenSize != tokenSize){
            _tokenSize = tokenSize;
            invalidateSize();
        }
    }

    public function set currentPlayer(currentPlayer:GamePlayer):void{
        _currentPlayer = currentPlayer;
        invalidateSize();
    }

    public function set startMoveHandler(startMoveHandler:Function):void{
        _startMoveHandler = startMoveHandler;
        invalidateProperties();
    }

    public function set endMoveHandler(endMoveHandler:Function):void{
        _endMoveHandler = endMoveHandler;
        invalidateProperties();
    }

    public function set active(active:Boolean):void{
        if (_active != active){
            _active = active;
            if (_active){
                activate();
            }else{
                desactivate();
            }
        }
    }

    public function addToken():void{
        if (_nTokens < INITIAL_N_TOKENS) {
            var token:Token = new Token();
            token.buttonMode = false;
            token.addEventListener(MouseEvent.MOUSE_OVER, selectToken);
            token.addEventListener(MouseEvent.MOUSE_OUT, unselectToken);
            if (_startMoveHandler != null){
                    token.addEventListener(MouseEvent.MOUSE_DOWN, _startMoveHandler);
                }
            if (_endMoveHandler != null){
                token.addEventListener(DragEvent.DRAG_COMPLETE, _endMoveHandler);
            }
            addChild(token);
            _nTokens ++;
        }
    }

    public function removeToken():void{
        if(numChildren > 0){
            removeChildAt(numChildren - 1);
            _nTokens --;
        }
    }


    protected function selectToken(event:MouseEvent):void{
        if (event.target is Token) {
         Token(event.target).selected = true;
        }
    }

    protected function unselectToken(event:MouseEvent):void{
        if (event.target is Token) {
         Token(event.target).selected = false;
        }
    }

    protected function activate():void{
        visible = true;
        var ap:AnimateProperty = new AnimateProperty(this);
        ap.property = "x";
        ap.fromValue = x;
        ap.toValue = 0;
        ap.duration = 300;
        ap.play();
        invalidateSize();
    }

    protected function desactivate():void{
        var ap:AnimateProperty = new AnimateProperty(this);
        ap.property = "x";
        ap.fromValue = x;
        ap.toValue = - width;
        ap.duration = 300;
        ap.addEventListener(EffectEvent.EFFECT_END, function():void{visible = false; invalidateSize()});
        ap.play();
    }

    override protected function createChildren():void{

        // Create background
        _bg = new Shape();
        addChild(_bg);


        // Create initial tokens
        var token:Token;
        _nTokens = 0;
        for (var i:int = 0; i < INITIAL_N_TOKENS; i++){
            addToken();
        }
    }

    override protected function commitProperties():void {

        //redefine handlers
        var token:Token;
        for (var i:Number = 0; i < _nTokens; i++){
            token = Token(getChildAt(i+ 1));
            if (_startMoveHandler != null){
               token.addEventListener(MouseEvent.MOUSE_DOWN, _startMoveHandler);
            }
            if (_endMoveHandler != null){
               token.addEventListener(DragEvent.DRAG_COMPLETE, _endMoveHandler);
            }
        }
    }

    override protected function measure():void{

        // Check that token size and current player have been defined
        if (_nTokens == 0 || isNaN(_tokenSize) || _tokenSize <= 0 || _currentPlayer == null){
            return
        }

        _tokensPerRow = Math.floor((unscaledWidth - 2 * PADDING - (_tokenSize * OVERLAP)) / (_tokenSize * (1 - OVERLAP)));

        if (!visible){
            measuredHeight = 0;
            measuredMinHeight = 0;
        }else{
            measuredHeight = PADDING * 2 + Math.ceil(_nTokens / _tokensPerRow) * (_tokenSize * (1 - OVERLAP)) + (_tokenSize * OVERLAP);
            measuredMinHeight = measuredHeight;
        }
    }

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{

        // Check that token size and current player have been defined
        if (_nTokens == 0 || isNaN(_tokenSize) || _tokenSize <= 0 || _currentPlayer == null){
            return
        }

        // Calculate number of tokens per row and height of background
        var token:Token;
        var baseX:Number = (unscaledWidth - 2 * PADDING - _tokensPerRow * _tokenSize * (1 - OVERLAP) - _tokenSize * OVERLAP) / 2;

        // Update background
        _bg.graphics.clear();
        _bg.graphics.beginFill(0xcccccc);
        _bg.graphics.lineStyle(1, 0xffffff, 3);
        _bg.graphics.drawRoundRect(0, 0, unscaledWidth, measuredHeight, CORNER_SIZE, CORNER_SIZE);
        _bg.graphics.endFill();

        //draw tokens
        var cell:Cell = new Cell();
        cell.color = _currentPlayer.color;
        for(var i:int = 0; i < _nTokens; i++){
            token = Token( getChildAt(i+ 1));
            token.width = _tokenSize;
            token.height = _tokenSize;
            token.x = baseX + PADDING + (i % _tokensPerRow) * _tokenSize * (1 - OVERLAP) + _tokenSize / 2;
            token.y = PADDING + Math.floor(i / _tokensPerRow) * _tokenSize * (1 - OVERLAP) + _tokenSize / 2;
            token.cell = cell;
        }

        if (_active){
            x = 0;
            visible = true;
        }else{
            x = - unscaledWidth;
            visible = false;
        }
    }

}
}