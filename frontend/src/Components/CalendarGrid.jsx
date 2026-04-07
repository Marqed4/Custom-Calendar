import sun from "../resources/assets/images/Days/Sun Inverted.gif";
import mon from "../resources/assets/images/Days/Mon Inverted.gif";
import tue from "../resources/assets/images/Days/Tue Inverted.gif";
import wed from "../resources/assets/images/Days/Wed Inverted.gif";
import thu from "../resources/assets/images/Days/Thu Inverted.gif";
import fri from "../resources/assets/images/Days/Fri Inverted.gif";
import sat from "../resources/assets/images/Days/Sat Inverted.gif";

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
import n10 from "../resources/assets/images/Numbers/10.gif";
import n11 from "../resources/assets/images/Numbers/11.gif";
import n12 from "../resources/assets/images/Numbers/12.gif";
import n13 from "../resources/assets/images/Numbers/13.gif";
import n14 from "../resources/assets/images/Numbers/14.gif";
import n15 from "../resources/assets/images/Numbers/15.gif";
import n16 from "../resources/assets/images/Numbers/16.gif";
import n17 from "../resources/assets/images/Numbers/17.gif";
import n18 from "../resources/assets/images/Numbers/18.gif";
import n19 from "../resources/assets/images/Numbers/19.gif";
import n20 from "../resources/assets/images/Numbers/20.gif";
import n21 from "../resources/assets/images/Numbers/21.gif";
import n22 from "../resources/assets/images/Numbers/22.gif";
import n23 from "../resources/assets/images/Numbers/23.gif";
import n24 from "../resources/assets/images/Numbers/24.gif";
import n25 from "../resources/assets/images/Numbers/25.gif";
import n26 from "../resources/assets/images/Numbers/26.gif";
import n27 from "../resources/assets/images/Numbers/27.gif";
import n28 from "../resources/assets/images/Numbers/28.gif";
import n29 from "../resources/assets/images/Numbers/29.gif";
import n30 from "../resources/assets/images/Numbers/30.gif";
import n31 from "../resources/assets/images/Numbers/31.gif";

import n0i from "../resources/assets/images/Numbers/0 Inverted.gif";
import n1i from "../resources/assets/images/Numbers/1 Inverted.gif";
import n2i from "../resources/assets/images/Numbers/2 Inverted.gif";
import n3i from "../resources/assets/images/Numbers/3 Inverted.gif";
import n4i from "../resources/assets/images/Numbers/4 Inverted.gif";
import n5i from "../resources/assets/images/Numbers/5 Inverted.gif";
import n6i from "../resources/assets/images/Numbers/6 Inverted.gif";
import n7i from "../resources/assets/images/Numbers/7 Inverted.gif";
import n8i from "../resources/assets/images/Numbers/8 Inverted.gif";
import n9i from "../resources/assets/images/Numbers/9 Inverted.gif";
import n10i from "../resources/assets/images/Numbers/10 Inverted.gif";
import n11i from "../resources/assets/images/Numbers/11 Inverted.gif";
import n12i from "../resources/assets/images/Numbers/12 Inverted.gif";
import n13i from "../resources/assets/images/Numbers/13 Inverted.gif";
import n14i from "../resources/assets/images/Numbers/14 Inverted.gif";
import n15i from "../resources/assets/images/Numbers/15 Inverted.gif";
import n16i from "../resources/assets/images/Numbers/16 Inverted.gif";
import n17i from "../resources/assets/images/Numbers/17 Inverted.gif";
import n18i from "../resources/assets/images/Numbers/18 Inverted.gif";
import n19i from "../resources/assets/images/Numbers/19 Inverted.gif";
import n20i from "../resources/assets/images/Numbers/20 Inverted.gif";
import n21i from "../resources/assets/images/Numbers/21 Inverted.gif";
import n22i from "../resources/assets/images/Numbers/22 Inverted.gif";
import n23i from "../resources/assets/images/Numbers/23 Inverted.gif";
import n24i from "../resources/assets/images/Numbers/24 Inverted.gif";
import n25i from "../resources/assets/images/Numbers/25 Inverted.gif";
import n26i from "../resources/assets/images/Numbers/26 Inverted.gif";
import n27i from "../resources/assets/images/Numbers/27 Inverted.gif";
import n28i from "../resources/assets/images/Numbers/28 Inverted.gif";
import n29i from "../resources/assets/images/Numbers/29 Inverted.gif";
import n30i from "../resources/assets/images/Numbers/30 Inverted.gif";
import n31i from "../resources/assets/images/Numbers/31 Inverted.gif";

import removeGif from "../resources/assets/images/ShapesSigns/remove.gif";

import "./CalendarGrid.css";

const DAY_GIFS = [sun, mon, tue, wed, thu, fri, sat];
const DAY_NAMES = ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"];
const DAY_NUMBERS = [n0,n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,n11,n12,n13,n14,n15,n16,n17,n18,n19,n20,n21,n22,n23,n24,n25,n26,n27,n28,n29,n30,n31];
const DAY_NUMBERS_INVERTED = [n0i,n1i,n2i,n3i,n4i,n5i,n6i,n7i,n8i,n9i,n10i,n11i,n12i,n13i,n14i,n15i,n16i,n17i,n18i,n19i,n20i,n21i,n22i,n23i,n24i,n25i,n26i,n27i,n28i,n29i,n30i,n31i];

export default function CalendarGrid({ calendarDays, currentDate, alarms, onDayClick, onDeleteAlarm, gridSize }) {
  return (
    <div className="calendar-grid" style={{ width: gridSize, height: gridSize }}>

      {DAY_GIFS.map((gif, i) => (
        <div key={i} className="day">
          <img src={gif} alt={DAY_NAMES[i]} className="day-gif" />
        </div>
      ))}

      {calendarDays.map((date, i) => {
        const isToday = date && date.toDateString() === new Date().toDateString();
        return (
          <div
            key={i}
            className={`day-cell ${isToday ? "today" : ""}`}
            onClick={() => onDayClick(date)}
          >
            {date && (
              <>
                <img
                  src={isToday ? DAY_NUMBERS_INVERTED[date.getDate()] : DAY_NUMBERS[date.getDate()]}
                  alt={date.getDate()}
                  className="day-number-img"
                />
                {alarms
                  .filter(a => a.time?.startsWith(date.toISOString().split("T")[0]))
                  .map((alarm, idx) => (
                    <div key={idx} className="alarm">
                      <span className="alarm-title">{alarm.title}</span>
                      <span className="alarm-time">
                        {new Date(alarm.time).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                      </span>
                      <img
                        src={removeGif}
                        alt="Remove"
                        className="alarm-delete"
                        onClick={(e) => { e.stopPropagation(); onDeleteAlarm(alarm); }}
                      />
                    </div>
                  ))
                }
              </>
            )}
          </div>
        );
      })}
    </div>
  );
}