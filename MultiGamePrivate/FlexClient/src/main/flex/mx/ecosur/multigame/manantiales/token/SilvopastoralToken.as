package mx.ecosur.multigame.manantiales.token
{   	
	import mx.ecosur.multigame.component.Token;
	
    import mx.ecosur.multigame.manantiales.enum.TokenType;
	import mx.ecosur.multigame.enum.Color;

	public class SilvopastoralToken extends ManantialesToken
	{
		public function SilvopastoralToken()
		{
			super();
			_tooltip = "Silvepastoral Active";
			_label = "S";
			_type = TokenType.SILVOPASTORAL;
		}
	}
}