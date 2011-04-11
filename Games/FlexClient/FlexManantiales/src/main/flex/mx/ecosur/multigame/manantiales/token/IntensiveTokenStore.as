package mx.ecosur.multigame.manantiales.token
{

    import flash.events.MouseEvent;    
    import mx.events.DragEvent;
    import mx.ecosur.multigame.enum.manantiales.TokenType;

    public class IntensiveTokenStore extends ManantialesTokenStore
    {
        public function IntensiveTokenStore()
        {
            super();
            _tokenType = TokenType.INTENSIVE;
        }

        public override function addToken():void{
            var token:IntensiveToken = new IntensiveToken();
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
        
        public function returnToken (dragEvent:DragEvent):void {
            var token:ManantialesToken = ManantialesToken (dragEvent.dragSource.dataForFormat("token"));
            if (token.ficha.type == TokenType.INTENSIVE) {
                addToken();
            }
        }
    }
}