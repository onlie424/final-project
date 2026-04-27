const BASE_URL = "http://localhost:8080/api/dashboard";

export async function fetchAvailableCourses(userId) {
  const res = await fetch(`${BASE_URL}/${userId}/available-courses`);
  if (!res.ok) throw new Error("Failed to load available courses");
  return res.json();
}
