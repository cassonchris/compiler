package casson.parser.symbols;

public enum Epsilon implements Terminal {
    E;
    
    @Override
    public String toString() {
        return "<" + this.name() + ">";
    }
}
