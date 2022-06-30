package net.openhft.chronicle.wire;
import net.openhft.chronicle.core.pool.ClassAliasPool;

public class LongConversionExampleB {
    static {
        ClassAliasPool.CLASS_ALIASES.addAlias(LongConversionExampleB.House.class);
    }
    public static class House extends SelfDescribingMarshallable{
        @LongConversion(Base64LongConverter.class)
        long owner;

        public void owner(CharSequence owner) {
            this.owner = Base64LongConverter.INSTANCE.parse(owner);
        }
    }
    public static void main(String[] args) {
        House house = new House();
        house.owner("Bill");
        System.out.println(house);
    }
}
