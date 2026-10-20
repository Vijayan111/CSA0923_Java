abstract class V { protected abstract double ratePerHour(); }
class C extends V {
    @Override protected double ratePerhour() { return 30; }   // typo: lower-case h
}
