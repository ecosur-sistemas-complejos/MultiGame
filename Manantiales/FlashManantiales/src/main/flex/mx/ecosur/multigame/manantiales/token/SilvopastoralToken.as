package mx.ecosur.multigame.manantiales.token
{

import mx.ecosur.multigame.manantiales.ManantialesToken;
import mx.ecosur.multigame.manantiales.enum.TokenType;
	import mx.ecosur.multigame.enum.Color;

    [ResourceBundle("Manantiales")]
	public class SilvopastoralToken extends ManantialesToken
	{
		public function SilvopastoralToken()
		{
			super();
			_tooltip = resourceManager.getString("Manantiales", "manantiales.token.silvopastoral");
			_label = "S";
			_type = TokenType.SILVOPASTORAL;
		}
	}
}
