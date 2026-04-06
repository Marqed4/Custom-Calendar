import jan from "../resources/assets/images/Months/January.gif";
import feb from "../resources/assets/images/Months/February.gif";
import mar from "../resources/assets/images/Months/March.gif";
import apr from "../resources/assets/images/Months/April.gif";
import may from "../resources/assets/images/Months/May.gif";
import jun from "../resources/assets/images/Months/June.gif";
import jul from "../resources/assets/images/Months/July.gif";
import aug from "../resources/assets/images/Months/August.gif";
import sep from "../resources/assets/images/Months/September.gif";
import oct from "../resources/assets/images/Months/October.gif";
import nov from "../resources/assets/images/Months/November.gif";
import dec from "../resources/assets/images/Months/December.gif";

import n0 from "../resources/assets/images/Numbers/0.gif";
import n1 from "../resources/assets/images/Numbers/1.gif";
import n2 from "../resources/assets/images/Numbers/2.gif";
import n3 from "../resources/assets/images/Numbers/3.gif";
import n4 from "../resources/assets/images/Numbers/4.gif";
import n5 from "../resources/assets/images/Numbers/5.gif";
import n6 from "../resources/assets/images/Numbers/6.gif";
import n7 from "../resources/assets/images/Numbers/7.gif";
import n8 from "../resources/assets/images/Numbers/8.gif";
import n9 from "../resources/assets/images/Numbers/9.gif";

import leftarrow from "../resources/assets/images/ShapesSigns/Left Arrow.gif";
import rightarrow from "../resources/assets/images/ShapesSigns/Right Arrow.gif";

const MONTHS = [jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec];
const NUMBERS = [n0, n1, n2, n3, n4, n5, n6, n7, n8, n9];

export default function MonthYearDisplay({ currentDate, onPrev, onNext }) {
  return (
    <div className="top-nav">
      <img src={leftarrow} alt="Previous" className="nav-arrow" onClick={onPrev} />
      <div className="main-month">
        <img
          src={MONTHS[currentDate.getMonth()]}
          alt={currentDate.toLocaleString("default", { month: "long" })}
          className="month-gif"
        />
        <div className="year-gifs">
          {String(currentDate.getFullYear()).split("").map((digit, i) => (
            <img key={i} src={NUMBERS[parseInt(digit)]} alt={digit} className="number-gif" />
          ))}
        </div>
      </div>
      <img src={rightarrow} alt="Next" className="nav-arrow" onClick={onNext} />
    </div>
  );
}