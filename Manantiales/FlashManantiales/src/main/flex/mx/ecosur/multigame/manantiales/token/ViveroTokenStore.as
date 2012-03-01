package mx.ecosur.multigame.manantiales.token
{
    import flash.events.MouseEvent;

import mx.ecosur.multigame.manantiales.ManantialesToken;

import mx.events.DragEvent;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
	
	public class ViveroTokenStore extends ManantialesTokenStore
	{
		
		public function ViveroTokenStore()
		{
			super();
			_tokenType = TokenType.VIVERO;
		}
		
        public override function addToken():void{
            var token:ViveroToken = new ViveroToken();
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
            if (token.ficha.type == TokenType.VIVERO) {
                addToken();
            }
        }
	}
}
