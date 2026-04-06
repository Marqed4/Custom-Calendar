import { BrowserRouter as Router, Routes, Route } from "react-router-dom";;
import Home from "./Components/Home.jsx";
import AddAlarm from "./Components/AddAlarm.jsx";
import "./App.css";

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/add-alarm" element={<AddAlarm />} />
      </Routes>
    </Router>
  );
}