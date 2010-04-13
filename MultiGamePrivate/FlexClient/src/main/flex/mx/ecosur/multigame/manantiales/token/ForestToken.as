package mx.ecosur.multigame.manantiales.token
{
    import mx.controls.Image;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    
    public class ForestToken extends ManantialesToken
    {
    public function ForestToken () {
        super ();
        _tooltip = "Managed Forest";
        _label = "F";
        _type = TokenType.FOREST;
        }
    }
}