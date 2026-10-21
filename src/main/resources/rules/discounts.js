// Volume discount: a rate on the order subtotal, plus a bonus for large unit counts.
function discountRate(subtotal, units) {
    var rate = 0;
    if (subtotal >= 1000) {
        rate = 0.10;
    } else if (subtotal >= 500) {
        rate = 0.05;
    }
    if (units >= 50) {
        rate = rate + 0.02;
    }
    return Math.round(rate * 100) / 100;
}
