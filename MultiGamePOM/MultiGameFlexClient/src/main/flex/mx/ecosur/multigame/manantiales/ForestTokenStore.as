package mx.ecosur.multigame.manantiales
{
	
    import flash.events.MouseEvent;
    
    import mx.events.DragEvent;
    
    import mx.ecosur.multigame.component.Token;
	
	public class ForestTokenStore extends ManantialesTokenStore
	{
		public function ForestTokenStore()
		{
			super();
		}
		
		public override function addToken():void{
            var token:Token = new ForestToken();
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
}